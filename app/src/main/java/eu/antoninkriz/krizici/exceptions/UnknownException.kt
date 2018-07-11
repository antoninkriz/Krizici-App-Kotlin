package eu.antoninkriz.krizici.exceptions

class UnknownException : CustomExceptions {
    internal constructor() : super()

    internal constructor(message: String) : super(message)

    constructor(message: String, code: Int) : super(message, code)
}
