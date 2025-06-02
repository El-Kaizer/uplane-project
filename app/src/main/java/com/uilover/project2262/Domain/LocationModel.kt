package com.uilover.project2262.Domain

data class LocationModel(
    private var _key: String? = null,
    private var _id: String? = null,
    private var _city: String = "",
    private var _code: String = "",
    private var _latitude: Double = 0.0,
    private var _longitude: Double = 0.0,
    private var _name: String = "",
    private var _region: String = ""
) {
    var key: String?
        get() = _key
        set(value) {
            _key = value
        }

    var id: String?
        get() = _id
        set(value) {
            _id = value
        }

    var city: String
        get() = _city
        set(value) {
            _city = value
        }

    var code: String
        get() = _code
        set(value) {
            _code = value
        }

    var latitude: Double
        get() = _latitude
        set(value) {
            _latitude = value
        }

    var longitude: Double
        get() = _longitude
        set(value) {
            _longitude = value
        }

    var name: String
        get() = _name
        set(value) {
            _name = value
        }

    var region: String
        get() = _region
        set(value) {
            _region = value
        }
}
