import kotlinx.serialization.Serializable

@Serializable
class Offer {
    var id: String? = null
    var title: String? = null
    var location: String? = null
    var price: String? = null
    var square: String? = null
    var url: String? = null
    var pictureUrl: String? = null
}