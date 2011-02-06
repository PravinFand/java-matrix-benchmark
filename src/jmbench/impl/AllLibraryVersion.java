/*
 * Copyright (c) 2009-2010, Peter Abeles. All Rights Reserved.
 *
 * This file is part of JMatrixBenchmark.
 *
 * JMatrixBenchmark is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JMatrixBenchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JMatrixBenchmark.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmbench.impl;

import org.ejml.UtilEjml;
import org.ojalgo.OjAlgoUtils;

/**
 *
 * A single place containing version information on all the supported libraries
 *
 * @author Peter Abeles
 */
public class AllLibraryVersion {

    public static class Colt implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.2";
        }

        @Override
        public String getReleaseDate() {
            return "";
        }
    }

    public static class COMMONS implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "2.1";
        }

        @Override
        public String getReleaseDate() {
            return "2010-04-05";
        }
    }

    public static class PColt implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "0.9.4";
        }

        @Override
        public String getReleaseDate() {
            return "2010-03-20";
        }
    }

    public static class JBLAS implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.2.0";
        }

        @Override
        public String getReleaseDate() {
            return "2011-01-07";
        }
    }

    public static class JAMA implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.0.2";
        }

        @Override
        public String getReleaseDate() {
            return "";
        }
    }

    public static class EJML implements LibraryVersion {

        @Override
        public String getVersionString() {
            return UtilEjml.VERSION;
        }

        @Override
        public String getReleaseDate() {
            return "2010-10-19";
        }
    }

    public static class MTJ implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "0.9.13";
        }

        @Override
        public String getReleaseDate() {
            return "2010-11-12";
        }
    }


    public static class OJALGO implements LibraryVersion {

        @Override
        public String getVersionString() {
            return OjAlgoUtils.getVersion();
        }

        @Override
        public String getReleaseDate() {
            return "2011-02-01";
        }
    }

    public static class UJMP implements LibraryVersion {

        @Override
        public String getVersionString() {
            return org.ujmp.core.UJMP.UJMPVERSION;
        }

        @Override
        public String getReleaseDate() {
            return "2010-06-22";
        }
    }

}
