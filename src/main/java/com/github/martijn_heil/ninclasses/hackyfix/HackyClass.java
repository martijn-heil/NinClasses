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

package com.github.martijn_heil.ninclasses.hackyfix;


import com.github.martijn_heil.ninclasses.NinClasses;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;

public class HackyClass
{
    public static boolean doStuff(String dbUrl, String dbUsername, String dbPassword, ClassLoader pluginLoader) {
        Flyway flyway = new Flyway();
        flyway.setClassLoader(pluginLoader);
        flyway.setDataSource(dbUrl, dbUsername, dbPassword);
        flyway.setBaselineOnMigrate(true);
        flyway.setBaselineVersion(MigrationVersion.fromVersion("0"));

        try {
            flyway.migrate();
        } catch (FlywayException ex) {
            NinClasses.instance.getLogger().severe(ex.getMessage());
            NinClasses.instance.getLogger().fine(ExceptionUtils.getFullStackTrace(ex));
            NinClasses.instance.getLogger().severe("Repairing migrations and disabling plugin..");
            try {
                flyway.repair();
            } catch (FlywayException ex2) {
                NinClasses.instance.getLogger().severe("Error whilst attempting to repair migrations:");
                ex2.printStackTrace();
                NinClasses.instance.getLogger().severe("Continuing to disable plugin..");
            }
            return false;
        }

        return true;
    }
}
