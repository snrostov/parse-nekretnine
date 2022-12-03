import geocode.Location
import kotlinx.serialization.Serializable

@Serializable
class Offer {
    var id: String? = null
    var title: String? = null
    var location: String? = null
    var locationData: Location? = null
    var price: String? = null
    var square: String? = null
    var rooms: String? = null
    var heating: String? = null
    var url: String? = null
    var pictureUrl: String? = null
    var pictureFile: String? = null
    var description: String? = null
}