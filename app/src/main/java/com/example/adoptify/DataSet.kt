package com.example.adoptify

data class DataSet(
    val petid:Int,
    val petname:String,
    val petbreed:String,
    val petage:Int,
    val petgender:String,
    val petcontact:Int,
    val petlocation:String,
    val petphoto:ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataSet

        return petphoto.contentEquals(other.petphoto)
    }

    override fun hashCode(): Int {
        return petphoto.contentHashCode()
    }
}
