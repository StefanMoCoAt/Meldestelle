import at.mocode.masterdata.ui.components.StammdatenListe
import react.create

/**
 * Main entry point for the Master Data JavaScript build.
 *
 * This function serves as the entry point for the Kotlin/JS application.
 * It registers the React component as a web component using r2wc.
 */
fun main() {
    console.log("Master Data JS module loaded successfully!")

    // Import r2wc function from @r2wc/react-to-web-component npm package
    val r2wc = js("require('@r2wc/react-to-web-component')")

    // Convert React component to Web Component using r2wc
    val StammdatenListeWebComponent = r2wc(StammdatenListe, js("{}"))

    // Register the new component with a custom HTML tag
    js("customElements.define('stammdaten-liste', arguments[0])")(StammdatenListeWebComponent)

    console.log("Web component 'stammdaten-liste' registered successfully!")
    console.log("You can now use <stammdaten-liste></stammdaten-liste> in your HTML")
}
