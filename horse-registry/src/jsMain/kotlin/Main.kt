import at.mocode.horses.ui.components.PferdeListe
import react.create

/**
 * Main entry point for the Horse Registry JavaScript build.
 *
 * This function serves as the entry point for the Kotlin/JS application.
 * It registers the React component as a web component using r2wc.
 */
fun main() {
    console.log("Horse Registry JS module loaded successfully!")

    // Import r2wc function from @r2wc/react-to-web-component npm package
    val r2wc = js("require('@r2wc/react-to-web-component')")

    // Convert React component to Web Component using r2wc
    val PferdeListeWebComponent = r2wc(PferdeListe, js("{}"))

    // Register the new component with a custom HTML tag
    js("customElements.define('pferde-liste', arguments[0])")(PferdeListeWebComponent)

    console.log("Web component 'pferde-liste' registered successfully!")
    console.log("You can now use <pferde-liste></pferde-liste> in your HTML")
}
