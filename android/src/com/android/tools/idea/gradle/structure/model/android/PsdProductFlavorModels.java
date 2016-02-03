/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.structure.model.android;

import com.android.annotations.NonNull;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.tools.idea.gradle.dsl.model.GradleBuildModel;
import com.android.tools.idea.gradle.dsl.model.android.ProductFlavorModel;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

class PsdProductFlavorModels {
  @NotNull private final Map<String, PsdProductFlavorModel> myProductFlavorModelsByName = Maps.newHashMap();

  PsdProductFlavorModels(@NonNull PsdAndroidModuleModel parent) {
    Map<String, ProductFlavor> productFlavorsFromGradle = Maps.newHashMap();
    for (ProductFlavorContainer container : parent.getGradleModel().getAndroidProject().getProductFlavors()) {
      ProductFlavor productFlavor = container.getProductFlavor();
      productFlavorsFromGradle.put(productFlavor.getName(), productFlavor);
    }

    GradleBuildModel parsedModel = parent.getParsedModel();
    if (parsedModel != null) {
      Collection<ProductFlavorModel> parsedProductFlavors = parsedModel.android().productFlavors();
      for (ProductFlavorModel parsedProductFlavor : parsedProductFlavors) {
        String name = parsedProductFlavor.name();
        ProductFlavor fromGradle = productFlavorsFromGradle.remove(name);

        PsdProductFlavorModel model = new PsdProductFlavorModel(parent, fromGradle, parsedProductFlavor);
        myProductFlavorModelsByName.put(name, model);
      }
    }

    if (!productFlavorsFromGradle.isEmpty()) {
      for (ProductFlavor productFlavor : productFlavorsFromGradle.values()) {
        PsdProductFlavorModel model = new PsdProductFlavorModel(parent, productFlavor, null);
        myProductFlavorModelsByName.put(productFlavor.getName(), model);
      }
    }
  }

  @NotNull
  Collection<PsdProductFlavorModel> getValues() {
    return myProductFlavorModelsByName.values();
  }

  @Nullable
  PsdProductFlavorModel findProductFlavorModel(@NotNull String productFlavorName) {
    return myProductFlavorModelsByName.get(productFlavorName);
  }
}
