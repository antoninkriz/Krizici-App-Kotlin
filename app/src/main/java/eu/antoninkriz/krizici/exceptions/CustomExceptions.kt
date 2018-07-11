package eu.antoninkriz.krizici.exceptions

abstract class CustomExceptions : Exception {
    private val msg: String?
    private val code: Int

    protected constructor() {
        this.msg = null
        this.code = 0
    }

    protected constructor(message: String) {
        this.msg = message
        this.code = 0
    }

    protected constructor(code: Int) {
        this.msg = null
        this.code = code
    }

    protected constructor(message: String, code: Int) {
        this.msg = message
        this.code = code
    }

    fun getExceptionMessage(): String {
        return this.javaClass.name + "; Message: " + msg + "; Code" + code
    }
}


