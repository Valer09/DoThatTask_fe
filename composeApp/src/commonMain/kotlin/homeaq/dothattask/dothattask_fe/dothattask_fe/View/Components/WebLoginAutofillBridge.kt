package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.runtime.Composable

/**
 * Compose Multiplatform on Wasm/JS renders text fields into a `<canvas>`
 * element, so browser password managers can't see them. To still get a real
 * autofill / save-credentials experience, we mount a tiny hidden HTML
 * `<form>` next to the canvas with the standard
 * `autocomplete="username"` / `autocomplete="current-password"` attributes.
 *
 * Two-way wiring with the visible Compose login fields:
 *  - When the user types in the Compose field, [username] / [password] update
 *    and the bridge mirrors them back into the hidden `<input>` elements so a
 *    submit triggered later carries the right values.
 *  - When the user picks a suggestion from the password manager, the
 *    HTML inputs receive `input`/`change` events; the bridge forwards them
 *    via [onUsernameChange] / [onPasswordChange] so the visible Compose
 *    field updates and the user can review the prefilled credentials.
 *
 * On a successful login the page bumps [submitTrigger]; the bridge calls
 * `form.requestSubmit()` so the browser's "save credentials" dialog kicks
 * in.
 *
 * No-op on every non-web target (Android/iOS already have native autofill
 * via [androidx.compose.ui.semantics.contentType], and desktop has no
 * password-manager equivalent).
 */
@Composable
expect fun WebLoginAutofillBridge(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    submitTrigger: Int,
)
