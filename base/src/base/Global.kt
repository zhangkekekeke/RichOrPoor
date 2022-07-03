package base

import java.io.File


class Global {
    companion object {
        val isApp = false
        val projectPath =if (isApp)
                    File(Global::class.java.protectionDomain.codeSource.location.file).absolutePath
                 else
                    File(Global::class.java.getResource("/").file).absolutePath


    }

}