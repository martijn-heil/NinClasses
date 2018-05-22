/*
 *     wac-core
 *     Copyright (C) 2016 Martijn Heil
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.martijn_heil.ninclasses

import org.apache.commons.io.IOUtils
import java.io.IOException




class HackyClassLoader(parent: ClassLoader) : ClassLoader(parent) {

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*>? {
        if (name == "org.flywaydb.core.internal.util.VersionPrinter") {
            try {
                val input = this.javaClass.getResourceAsStream("/VersionPrinter.class")
                val bytes = IOUtils.toByteArray(input)
                return defineClass(name, bytes, 0, bytes.size)
            } catch (e: IOException) {
                throw ClassNotFoundException("", e)
            }
        } else if (name == "tk.martijn_heil.wac_core.HackyClass") {
            try {
                val input = this.javaClass.getResourceAsStream("HackyClass.class")
                val bytes = IOUtils.toByteArray(input)
                return defineClass(name, bytes, 0, bytes.size)
            } catch (e: IOException) {
                throw ClassNotFoundException("", e)
            }
        } else if (name.startsWith("org.flywaydb")) {
            try {
                val classFile = name.replace('.', '/') + ".class"
                val input = parent.getResourceAsStream(classFile)
                val bytes = IOUtils.toByteArray(input)
                return defineClass(name, bytes, 0, bytes.size)
            } catch (e: IOException) {
                throw ClassNotFoundException("", e)
            }
        } else {
            return parent.loadClass(name)
        }
    }
}