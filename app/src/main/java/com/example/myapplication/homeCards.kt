package com.example.myapplication

// parent class for non-interactive cards
sealed class homeCards {

    // park info card child
    // shows park stats to visitors
    data class parkInfo (
        private var info: String
    ) : homeCards() {

        // default methods
        fun setInfo(i: String){
            this.info = i
        }

        fun getInfo(): String{
            return this.info
        }

        fun equals(otherParkInfo: parkInfo): Boolean{
            if (this.info != otherParkInfo.info){return false}
            return true
        }

    }


    // bird of the day child
    // shows unique bird of the day, including its name,
    // image, and description
    data class birdOfTheDay (
        private var birdName: String,
        private var birdImage: Int,
        private var birdDescription: String
    ): homeCards() {

        // default methods
        fun setName(n: String){
            this.birdName = n
        }

        fun setImage(i: Int){
            this.birdImage = i
        }

        fun setDesc(d: String){
            this.birdDescription = d
        }

        fun getName(): String{
            return this.birdName
        }

        fun getImage(): Int{
            return this.birdImage
        }

        fun getDesc(): String{
            return this.birdDescription
        }

        fun equals(other: birdOfTheDay): Boolean{
            if (this.birdName != other.birdName){return false}
            if (this.birdImage != other.birdImage){return false}
            if (this.birdDescription != other.birdDescription){return false}
            return true
        }

    }

    // image child (for bird, plant, park pictures)
    // just displays a single image
    data class otherImages (
        private var otherImage: Int
    ): homeCards() {

        // default methods
        fun setImage(i: Int){
            this.otherImage = i
        }

        fun getImage(): Int{
            return this.otherImage
        }

        fun equals(other: otherImages): Boolean{
            if (this.otherImage != other.otherImage){return false}
            return true
        }

    }

    // feature information card child
    // promotes one of the app's features
    // with additional information and an
    // image included
    data class infoCard (
        private var title: String,
        private var infoImage: Int,
        private var additionalInfo: String
    ): homeCards() {

        // default methods
        fun setTitle(t: String){
            this.title = t
        }

        fun setImage(i: Int){
            this.infoImage = i
        }

        fun setInfo(a: String){
            this.additionalInfo = a
        }

        fun getTitle(): String{
            return this.title
        }

        fun getImage(): Int{
            return this.infoImage
        }

        fun getInfo(): String{
            return this.additionalInfo
        }

        fun equals(other: infoCard): Boolean{
            if (this.title != other.title){return false}
            if (this.infoImage != other.infoImage){return false}
            if (this.additionalInfo != other.additionalInfo){return false}
            return true
        }
    }

    // help card child
    // used to store a card for the help screen
    data class helpCard (
        private var image: Int,
        private var help: String,
        private  var title: String
    ) : homeCards() {

        // default methods
        fun setHelp(t: String){
            this.help = t
        }

        fun getHelp(): String{
            return this.help
        }

        fun setImage(img: Int){
            this.image = img
        }

        fun getImage(): Int{
            return this.image
        }

        fun setTitle(t: String){
            this.title = t
        }

        fun getTitle(): String{
            return this.title
        }

        fun equals(otherHelp: helpCard): Boolean{
            if (this.help != otherHelp.help){return false}
            if (this.image != otherHelp.image){return false}
            if (this.title != otherHelp.title){return false}
            return true
        }

    }


}
