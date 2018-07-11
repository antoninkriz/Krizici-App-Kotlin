package eu.antoninkriz.krizici.exceptions.network

import eu.antoninkriz.krizici.exceptions.CustomExceptions

class NoInternetException : CustomExceptions {
    constructor() : super()

    constructor(message: String) : super(message)

    constructor(code: Int) : super(code)

    constructor(message: String, code: Int) : super(message, code)
}
