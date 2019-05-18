package org.nearbyshops.enduserappnew.API;

import okhttp3.ResponseBody;
import org.nearbyshops.enduserappnew.ModelEndPoints.ShopReviewThanksEndpoint;
import org.nearbyshops.enduserappnew.ModelReviewShop.ShopReviewThanks;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by sumeet on 2/4/16.
 */

public interface ShopReviewThanksService {

    @GET("/api/v1/ShopReviewThanks")
    Call<ShopReviewThanksEndpoint> getShopReviewThanks(
            @Query("ShopReviewID") Integer shopReviewID,
            @Query("EndUserID") Integer endUserID,
            @Query("ShopID") Integer shopID,
            @Query("SortBy") String sortBy,
            @Query("Limit") Integer limit, @Query("Offset") Integer offset,
            @Query("metadata_only") Boolean metaonly
    );


    @POST("/api/v1/ShopReviewThanks")
    Call<ShopReviewThanks> insertThanks(@Body ShopReviewThanks shopReviewThanks);

    @DELETE("/api/v1/ShopReviewThanks")
    Call<ResponseBody> deleteThanks(@Query("ShopReviewID") Integer shopReviewID,
                                    @Query("EndUserID") Integer endUserID
    );

}
