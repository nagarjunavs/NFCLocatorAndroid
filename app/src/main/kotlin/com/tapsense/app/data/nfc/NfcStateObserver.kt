package com.tapsense.app.data.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live "is NFC turned on" status for the Home/Settings status rows - a real broadcast
 * registration, not a one-shot check, so toggling NFC in system settings while the app is open
 * is reflected without the user needing to relaunch.
 *
 * The broadcast alone isn't sufficient: delivery to a backgrounded/cached process can be
 * throttled or missed on some OEMs, which is exactly the "status doesn't refresh when I come
 * back to the app" symptom. So this also re-checks on [Lifecycle.Event.ON_RESUME] via
 * [ProcessLifecycleOwner] - a supplementary, always-correct fallback that doesn't depend on
 * the broadcast having actually been delivered while backgrounded.
 */
@Singleton
class NfcStateObserver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    val isNfcSupported: Boolean get() = adapter != null

    val isEnabled: Flow<Boolean> = callbackFlow {
        val currentAdapter = adapter
        if (currentAdapter == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }

        trySend(currentAdapter.isEnabled)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(currentAdapter.isEnabled)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        val processLifecycle = ProcessLifecycleOwner.get().lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                trySend(currentAdapter.isEnabled)
            }
        }
        // Lifecycle add/removeObserver must happen on the main thread, regardless of which
        // dispatcher this flow is collected/cancelled from (awaitClose's callback isn't a
        // suspend function, so a plain Handler post is simpler here than a coroutine hop).
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post { processLifecycle.addObserver(lifecycleObserver) }

        awaitClose {
            context.unregisterReceiver(receiver)
            mainHandler.post { processLifecycle.removeObserver(lifecycleObserver) }
        }
    }.distinctUntilChanged()
}
