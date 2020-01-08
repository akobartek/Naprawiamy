package pl.sokolowskibartlomiej.naprawiamy.utils

interface ViewException {
    fun handleException(e: Throwable): Boolean
}