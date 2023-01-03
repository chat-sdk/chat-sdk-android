package smartadapter.extension

/**
 * Defining the smart extension or binder and is the basic interface to implement in extension libraries
 * to bind with [SmartRecyclerAdapter] on adapter creation. Fetch the extension easily with the [identifier].
 */
interface SmartExtensionIdentifier {

    /**
     * Identifies the extension.
     */
    val identifier: Any
}