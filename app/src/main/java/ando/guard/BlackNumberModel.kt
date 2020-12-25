package ando.guard

import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.util.*

class BlackNumberModel : LitePalSupport() {
    @Column(unique = true, defaultValue = "", nullable = false)
    var number: String? = null
    var mode: String? = null
    var updateData: Date? = null
}