/*
 *    Copyright (c) 2023, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens.webserver.api.accountlinking;

import com.google.gson.JsonObject;
import io.supertokens.Main;
import io.supertokens.StorageAndUserIdMapping;
import io.supertokens.authRecipe.AuthRecipe;
import io.supertokens.authRecipe.exception.AccountInfoAlreadyAssociatedWithAnotherPrimaryUserIdException;
import io.supertokens.authRecipe.exception.RecipeUserIdAlreadyLinkedWithPrimaryUserIdException;
import io.supertokens.featureflag.exceptions.FeatureNotEnabledException;
import io.supertokens.multitenancy.exception.BadPermissionException;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.Storage;
import io.supertokens.pluginInterface.emailpassword.exceptions.UnknownUserIdException;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.multitenancy.AppIdentifier;
import io.supertokens.pluginInterface.multitenancy.exceptions.TenantOrAppNotFoundException;
import io.supertokens.useridmapping.UserIdMapping;
import io.supertokens.useridmapping.UserIdType;
import io.supertokens.utils.SemVer;
import io.supertokens.webserver.InputParser;
import io.supertokens.webserver.WebserverAPI;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CreatePrimaryUserAPI extends WebserverAPI {

    public CreatePrimaryUserAPI(Main main) {
        super(main, RECIPE_ID.ACCOUNT_LINKING.toString());
    }

    @Override
    public String getPath() {
        return "/recipe/accountlinking/user/primary";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // API is app specific
        JsonObject input = InputParser.parseJsonObjectOrThrowError(req);
        String inputRecipeUserId = InputParser.parseStringOrThrowError(input, "recipeUserId", false);

        AppIdentifier appIdentifier = null;
        try {
            appIdentifier = this.getAppIdentifier(req);
        } catch (TenantOrAppNotFoundException e) {
            throw new ServletException(e);
        }
        Storage storage = null;

        try {
            String userId = inputRecipeUserId;
            StorageAndUserIdMapping mappingAndStorage =
                    enforcePublicTenantAndGetStorageAndUserIdMappingForAppSpecificApi(
                            req, inputRecipeUserId, UserIdType.ANY, true);
            storage = mappingAndStorage.storage;
            if (mappingAndStorage.userIdMapping != null) {
                userId = mappingAndStorage.userIdMapping.superTokensUserId;
            }

            AuthRecipe.CreatePrimaryUserResult result = AuthRecipe.createPrimaryUser(main, appIdentifier, storage,
                    userId);
            JsonObject response = new JsonObject();
            response.addProperty("status", "OK");
            response.addProperty("wasAlreadyAPrimaryUser", result.wasAlreadyAPrimaryUser);
            if (mappingAndStorage.userIdMapping != null) {
                result.user.setExternalUserId(mappingAndStorage.userIdMapping.externalUserId);
            } else {
                result.user.setExternalUserId(null);
            }
            response.add("user", result.user.toJson(getVersionFromRequest(req).greaterThanOrEqualTo(SemVer.v5_3)));
            super.sendJsonResponse(200, response, resp);
        } catch (StorageQueryException | TenantOrAppNotFoundException | FeatureNotEnabledException |
                 BadPermissionException e) {
            throw new ServletException(e);
        } catch (UnknownUserIdException e) {
            throw new ServletException(new BadRequestException("Unknown user ID provided"));
        } catch (AccountInfoAlreadyAssociatedWithAnotherPrimaryUserIdException e) {
            try {
                JsonObject response = new JsonObject();
                response.addProperty("status", "ACCOUNT_INFO_ALREADY_ASSOCIATED_WITH_ANOTHER_PRIMARY_USER_ID_ERROR");
                io.supertokens.pluginInterface.useridmapping.UserIdMapping result = UserIdMapping.getUserIdMapping(
                        appIdentifier, storage, e.primaryUserId,
                        UserIdType.SUPERTOKENS);
                if (result != null) {
                    response.addProperty("primaryUserId", result.externalUserId);
                } else {
                    response.addProperty("primaryUserId", e.primaryUserId);
                }
                response.addProperty("description", e.getMessage());
                super.sendJsonResponse(200, response, resp);
            } catch (StorageQueryException ex) {
                throw new ServletException(ex);
            }
        } catch (RecipeUserIdAlreadyLinkedWithPrimaryUserIdException e) {
            try {
                JsonObject response = new JsonObject();
                response.addProperty("status", "RECIPE_USER_ID_ALREADY_LINKED_WITH_PRIMARY_USER_ID_ERROR");
                io.supertokens.pluginInterface.useridmapping.UserIdMapping result = UserIdMapping.getUserIdMapping(
                        appIdentifier, storage, e.primaryUserId,
                        UserIdType.SUPERTOKENS);
                if (result != null) {
                    response.addProperty("primaryUserId", result.externalUserId);
                } else {
                    response.addProperty("primaryUserId", e.primaryUserId);
                }
                response.addProperty("description", e.getMessage());
                super.sendJsonResponse(200, response, resp);
            } catch (StorageQueryException ex) {
                throw new ServletException(ex);
            }
        }
    }
}
