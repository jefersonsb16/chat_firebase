package com.jefersonsalazar.testquicksas.model

class MessageChat {

    private var id: String? = null
    private var text: String? = null
    private var name: String? = null
    private var photoUrlUser: String? = null
    private var imageUrl: String? = null
    private var hour: Long? = null
    private var idUser: String? = null

    fun setId(id: String) {
        this.id = id
    }

    fun getId() : String? {
        return this.id
    }

    fun setText(text: String) {
        this.text = text
    }

    fun getText() : String? {
        return this.text
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getName() : String? {
        return this.name
    }

    fun setPhotoUrlUser(photoUrlUser: String) {
        this.photoUrlUser = photoUrlUser
    }

    fun getPhotoUrlUser() : String? {
        return this.photoUrlUser
    }

    fun setImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    fun getImageUrl() : String? {
        return this.imageUrl
    }

    fun setHour(hour: Long) {
        this.hour = hour
    }

    fun getHour() : Long? {
        return this.hour
    }

    fun setIdUser(idUser: String) {
        this.idUser = idUser
    }

    fun getIdUser() : String? {
        return this.idUser
    }
}