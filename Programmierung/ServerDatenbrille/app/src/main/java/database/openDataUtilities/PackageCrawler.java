/*
 * Copyright 2015 [Alexander Bendl, Brunauer Ferdinand, Milena Matic]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package database.openDataUtilities;

import java.util.ArrayList;

/**
 * Created by Alexander on 22.01.2015.
 */
public class PackageCrawler {
    private static ArrayList<String> packageIds;
    private static ArrayList<OpenDataPackage> openDataPackages;

    public static void execute(){
        //TODO: delete Temp
        temp();
        openDataPackages = new ArrayList<>();

        for(String packageId:packageIds){
            openDataPackages.add(OpenDataUtilities.getPackageById(packageId));
        }
        String temp;
        //TODO: add Packages to DB
    }

    public static void setPackageIds(ArrayList<String> packageIds) {
        PackageCrawler.packageIds = packageIds;
    }

    public static void temp(){
        packageIds = new ArrayList<>();
        packageIds.add("a5841caf-afe2-4f98-bb68-bd4899e8c9cb");
    }

}
