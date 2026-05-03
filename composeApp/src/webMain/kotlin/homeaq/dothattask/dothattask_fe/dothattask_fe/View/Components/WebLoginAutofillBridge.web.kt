package homeaq.dothattask.dothattask_fe.dothattask_fe.View.Components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLInputElement

private const val FORM_ID = "dtt-autofill-form"

private class AutofillFormHandle(
    val form: HTMLFormElement,
    val usernameInput: HTMLInputElement,
    val passwordInput: HTMLInputElement,
    val submitInput: HTMLInputElement,
)

@Composable
actual fun WebLoginAutofillBridge(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    submitTrigger: Int,
) {
    // Holder so the LaunchedEffects below can reach the DOM nodes built once
    // by the DisposableEffect. We can't close over the locals directly:
    // those live inside the effect's lambda scope.
    val handleHolder = remember { arrayOfNulls<AutofillFormHandle>(1) }

    DisposableEffect(Unit) {
        val form = document.createElement("form") as HTMLFormElement
        form.id = FORM_ID
        form.setAttribute("autocomplete", "on")
        form.setAttribute("aria-hidden", "true")
        // Off-canvas, but still in the layout tree — most password managers
        // skip display:none/visibility:hidden inputs but still scan
        // opacity:0 ones. The 1×1 footprint plus pointer-events:none means
        // the user can never accidentally tab into it.
        form.setAttribute(
            "style",
            "position:fixed;top:0;left:0;width:1px;height:1px;opacity:0;" +
                "pointer-events:none;overflow:hidden;",
        )
        // Submit must never navigate — we own the login flow from Compose.
        form.setAttribute("action", "javascript:void(0)")
        form.setAttribute("method", "post")

        val usernameInput = document.createElement("input") as HTMLInputElement
        usernameInput.type = "text"
        usernameInput.name = "username"
        usernameInput.setAttribute("autocomplete", "username")
        usernameInput.tabIndex = -1
        usernameInput.value = username

        val passwordInput = document.createElement("input") as HTMLInputElement
        passwordInput.type = "password"
        passwordInput.name = "password"
        passwordInput.setAttribute("autocomplete", "current-password")
        passwordInput.tabIndex = -1
        passwordInput.value = password

        // A submit input lets the password manager understand this is a
        // proper login form (not just a couple of stray inputs).
        val submit = document.createElement("input") as HTMLInputElement
        submit.type = "submit"
        submit.tabIndex = -1
        submit.setAttribute("style", "display:none;")
        submit.value = "Login"

        form.appendChild(usernameInput)
        form.appendChild(passwordInput)
        form.appendChild(submit)
        document.body?.appendChild(form)

        // We use the on{event} property setters rather than addEventListener
        // because they're typed identically across the Kotlin/JS and
        // Kotlin/Wasm DOM bindings (both expose `((Event) -> Unit)?`),
        // sidestepping the SAM-conversion mismatch on the external
        // EventListener interface. Each input only ever has one handler we
        // care about, so overwriting is fine.
        usernameInput.oninput = { onUsernameChange(usernameInput.value) }
        usernameInput.onchange = { onUsernameChange(usernameInput.value) }
        passwordInput.oninput = { onPasswordChange(passwordInput.value) }
        passwordInput.onchange = { onPasswordChange(passwordInput.value) }
        // Swallow the default navigation that a real submit would otherwise
        // trigger; we only care about the event firing so password managers
        // can offer to save credentials.
        form.onsubmit = { event -> event.preventDefault() }

        handleHolder[0] = AutofillFormHandle(form, usernameInput, passwordInput, submit)

        onDispose {
            usernameInput.oninput = null
            usernameInput.onchange = null
            passwordInput.oninput = null
            passwordInput.onchange = null
            form.onsubmit = null
            form.parentNode?.removeChild(form)
            handleHolder[0] = null
        }
    }

    // Mirror the visible Compose state into the hidden form so that any
    // submit (programmatic or user-driven via Enter) carries the right
    // values for the password manager to capture.
    LaunchedEffect(username) {
        val handle = handleHolder[0] ?: return@LaunchedEffect
        if (handle.usernameInput.value != username) handle.usernameInput.value = username
    }
    LaunchedEffect(password) {
        val handle = handleHolder[0] ?: return@LaunchedEffect
        if (handle.passwordInput.value != password) handle.passwordInput.value = password
    }

    // Bumping submitTrigger after a successful login fires the form's submit
    // event — that's the native "save credentials?" hook for browser
    // password managers. Clicking the hidden <input type="submit"> is
    // equivalent to form.requestSubmit() but is part of the universal
    // HTMLElement API, so it stays compatible across Kotlin/Wasm releases.
    // The form's own onsubmit cancels the default navigation.
    LaunchedEffect(submitTrigger) {
        if (submitTrigger <= 0) return@LaunchedEffect
        val handle = handleHolder[0] ?: return@LaunchedEffect
        runCatching { handle.submitInput.click() }
    }
}
