import at.mocode.members.ui.components.MitgliederListe
import at.mocode.members.ui.components.LoginForm
import react.create

/**
 * Main entry point for the Member Management JavaScript build.
 *
 * This function serves as the entry point for the Kotlin/JS application.
 * It registers the React components as web components using r2wc.
 */
fun main() {
    console.log("Member Management JS module loaded successfully!")

    // Import r2wc function from @r2wc/react-to-web-component npm package
    val r2wc = js("require('@r2wc/react-to-web-component')")

    // Convert MitgliederListe React component to Web Component using r2wc
    val MitgliederListeWebComponent = r2wc(MitgliederListe, js("{}"))

    // Register the MitgliederListe component with a custom HTML tag
    js("customElements.define('mitglieder-liste', arguments[0])")(MitgliederListeWebComponent)

    console.log("Web component 'mitglieder-liste' registered successfully!")

    // Convert LoginForm React component to Web Component using r2wc
    // Define props configuration for the LoginForm component
    val loginFormProps = js("{}")
    js("loginFormProps.onLoginSuccess = { type: Function }")

    val LoginFormWebComponent = r2wc(LoginForm, loginFormProps)

    // Register the LoginForm component with a custom HTML tag
    js("customElements.define('login-form', arguments[0])")(LoginFormWebComponent)

    console.log("Web component 'login-form' registered successfully!")
    console.log("You can now use <mitglieder-liste></mitglieder-liste> and <login-form></login-form> in your HTML")
}
