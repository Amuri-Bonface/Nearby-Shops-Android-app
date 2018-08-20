package org.nearbyshops.enduserappnew.ItemsByCategoryTypeSimple;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wunderlist.slidinglayer.SlidingLayer;

import org.nearbyshops.enduserappnew.DaggerComponentBuilder;
import org.nearbyshops.enduserappnew.FilterItemsBySpecifications.FilterItemsActivity;
import org.nearbyshops.enduserappnew.Items.SlidingLayerSort.SlidingLayerSortItems;
import org.nearbyshops.enduserappnew.ItemsByCategoryTypeSimple.Interfaces.NotifyBackPressed;
import org.nearbyshops.enduserappnew.ItemsByCategoryTypeSimple.Interfaces.NotifyHeaderChanged;
import org.nearbyshops.enduserappnew.ItemsByCategoryTypeSimple.Utility.HeaderItemsList;
import org.nearbyshops.enduserappnew.Model.Item;
import org.nearbyshops.enduserappnew.Model.ItemCategory;
import org.nearbyshops.enduserappnew.ModelEndPoints.ItemCategoryEndPoint;
import org.nearbyshops.enduserappnew.ModelEndPoints.ItemEndPoint;
import org.nearbyshops.enduserappnew.R;
import org.nearbyshops.enduserappnew.RetrofitRESTContract.ItemCategoryService;
import org.nearbyshops.enduserappnew.RetrofitRESTContract.ItemService;
import org.nearbyshops.enduserappnew.Shops.UtilityLocation;
import org.nearbyshops.enduserappnew.ShopsByCategoryOld.Interfaces.NotifySort;
import org.nearbyshops.enduserappnew.Items.SlidingLayerSort.UtilitySortItemsByCategory;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.nearbyshops.enduserappnew.ItemsByCategoryTypeSimple.ItemCategoriesSimple.TAG_SLIDING;

/**
 * Created by sumeet on 2/12/16.
 */

public class ItemCategoriesFragmentSimple extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterSimple.NotificationsFromAdapter , NotifyBackPressed , NotifySort{

    boolean isDestroyed = false;

    int item_count_item_category = 0;

    private int limit_item = 10;
    int offset_item = 0;
    int item_count_item;
    int fetched_items_count = 0;

    @BindView(R.id.swipe_container) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.recycler_view) RecyclerView itemCategoriesList;

    ArrayList<Object> dataset = new ArrayList<>();
    ArrayList<ItemCategory> datasetCategory = new ArrayList<>();
    ArrayList<Item> datasetItems = new ArrayList<>();


    GridLayoutManager layoutManager;
    AdapterSimple listAdapter;

    @Inject ItemCategoryService itemCategoryService;
    @Inject ItemService itemService;



    @BindView(R.id.shop_count_indicator) TextView itemHeader;
    @BindView(R.id.slidingLayer) SlidingLayer slidingLayer;


    ItemCategory currentCategory = null;

    public ItemCategoriesFragmentSimple() {
        super();

        DaggerComponentBuilder.getInstance()
                .getNetComponent().Inject(this);

        currentCategory = new ItemCategory();
        currentCategory.setItemCategoryID(1);
        currentCategory.setCategoryName("");
        currentCategory.setParentCategoryID(-1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_item_categories_simple, container, false);
        ButterKnife.bind(this,rootView);


//        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        toolbar.setTitle("Nearby Shops");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//



        if(savedInstanceState ==null)
        {
            makeRefreshNetworkCall();
        }


        setupRecyclerView();
        setupSwipeContainer();
        notifyItemHeaderChanged();





        setupSlidingLayer();


        return rootView;
    }


    void setupSlidingLayer()
    {

        ////slidingLayer.setShadowDrawable(R.drawable.sidebar_shadow);
        //slidingLayer.setShadowSizeRes(R.dimen.shadow_size);

        if(slidingLayer!=null)
        {
            slidingLayer.setChangeStateOnTap(true);
            slidingLayer.setSlidingEnabled(true);
//            slidingLayer.setPreviewOffsetDistance(15);
            slidingLayer.setOffsetDistance(30);
            slidingLayer.setStickTo(SlidingLayer.STICK_TO_RIGHT);

            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(250, ViewGroup.LayoutParams.MATCH_PARENT);

            //slidingContents.setLayoutParams(layoutParams);

            //slidingContents.setMinimumWidth(metrics.widthPixels-50);



            if(getChildFragmentManager().findFragmentByTag(TAG_SLIDING)==null)
            {
                System.out.println("Item Cat Simple : New Sliding Layer Loaded !");
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.slidinglayerfragment,new SlidingLayerSortItems(),TAG_SLIDING)
                        .commit();
            }
        }

    }





    @OnClick({R.id.icon_sort,R.id.text_sort})
    void sortClick()
    {
        slidingLayer.openLayer(true);
    }


    @OnClick({R.id.icon_filter,R.id.text_filter})
    void filterClick()
    {
        Intent intent = new Intent(getActivity(), FilterItemsActivity.class);
        startActivity(intent);
    }



    void setupSwipeContainer()
    {

        if(swipeContainer!=null) {

            swipeContainer.setOnRefreshListener(this);
            swipeContainer.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

    }




    void setupRecyclerView()
    {

        listAdapter = new AdapterSimple(dataset,getActivity(),this,this);
        itemCategoriesList.setAdapter(listAdapter);

        layoutManager = new GridLayoutManager(getActivity(),6, LinearLayoutManager.VERTICAL,false);
        itemCategoriesList.setLayoutManager(layoutManager);



        // Code for Staggered Grid Layout
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {


            @Override
            public int getSpanSize(int position) {



                if(position == dataset.size())
                {

                }
                else if(dataset.get(position) instanceof ItemCategory)
                {
                    final DisplayMetrics metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                    int spanCount = (int) (metrics.widthPixels/(180 * metrics.density));

                    if(spanCount==0){
                        spanCount = 1;
                    }

                    return (6/spanCount);

                }
                else if(dataset.get(position) instanceof Item)
                {

                    return 6;
                }
                else if(dataset.get(position) instanceof HeaderItemsList)
                {
                    return 6;
                }

                return 6;
            }
        });


        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        layoutManager.setSpanCount(metrics.widthPixels/350);


//        int spanCount = (int) (metrics.widthPixels/(150 * metrics.density));
//
//        if(spanCount==0){
//            spanCount = 1;
//        }

//        layoutManager.setSpanCount(spanCount);


        /*final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int spanCount = (int) (metrics.widthPixels/(180 * metrics.density));

        if(spanCount==0){
            spanCount = 1;
        }

        return (3/spanCount);*/


        itemCategoriesList.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                /*if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1)
                {
                    // trigger fetch next page

                    if((offset_item + limit_item)<=item_count_item)
                    {
                        offset_item = offset_item + limit_item;

                        makeRequestItem(false,false);
                    }

                }
*/


                if(offset_item + limit_item > layoutManager.findLastVisibleItemPosition())
                {
                    return;
                }



                if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1+1)
                {

                    // trigger fetch next page

//
//                    String log = "Dataset Size : " + String.valueOf(dataset.size()) + "\n"
//                            + "Last Visible Item Position : " + layoutManager.findLastVisibleItemPosition() + "\n"
//                            + "Previous Position : " + previous_position + "\n"
//                            + "Offset Item : " + offset_item + "\n"
//                            + "Limit Item : " + limit_item + "\n"
//                            + "Item Count Item : " + item_count_item;

//                    System.out.println(log);
//                    Log.d("log_scroll",log);




                    if(layoutManager.findLastVisibleItemPosition()== previous_position)
                    {
                        return;
                    }


                    // trigger fetch next page

                    if((offset_item + limit_item)<=item_count_item)
                    {
                        offset_item = offset_item + limit_item;

                        makeRequestItem(false,false);
                    }

                    previous_position = layoutManager.findLastVisibleItemPosition();

                }
            }

        });

    }


    int previous_position = -1;


    void resetPreviousPosition()
    {
        previous_position = -1;
    }



    @Override
    public void onRefresh() {

        makeRequestItemCategory();
        makeRequestItem(true,true);
    }



    void makeRefreshNetworkCall()
    {
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {

                swipeContainer.setRefreshing(true);
                onRefresh();
            }
        });

    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isDestroyed=false;
    }

    private void showToastMessage(String message)
    {
        if(getActivity()!=null)
        {
            Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
        }
    }



    boolean isFirst = true;


    void makeRequestItemCategory()
    {
//        (double) UtilityGeneral.getFromSharedPrefFloat(UtilityGeneral.LAT_CENTER_KEY, 0),
//                (double) UtilityGeneral.getFromSharedPrefFloat(UtilityGeneral.LON_CENTER_KEY, 0),

         Call<ItemCategoryEndPoint> endPointCall = itemCategoryService.getItemCategoriesEndPoint(
                null,
                currentCategory.getItemCategoryID(),
                null,
                UtilityLocation.getLatitude(getActivity()),
                UtilityLocation.getLongitude(getActivity()),
                null,null,null,
                true,
                ItemCategory.CATEGORY_ORDER,null,null,false);


        endPointCall.enqueue(new Callback<ItemCategoryEndPoint>() {
            @Override
            public void onResponse(Call<ItemCategoryEndPoint> call, Response<ItemCategoryEndPoint> response) {

                if(isDestroyed)
                {
                    return;
                }

                if(response.body()!=null)
                {

                    ItemCategoryEndPoint endPoint = response.body();
                    item_count_item_category = endPoint.getItemCount();

                    datasetCategory.clear();
                    datasetCategory.addAll(endPoint.getResults());
                }


                if(isFirst)
                {
                    isFirst = false;
                }
                else
                {
                    // is last
                    refreshAdapter();
                    isFirst = true;// reset the flag
                }


//                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ItemCategoryEndPoint> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }

                showToastMessage("Network request failed. Please check your connection !");


                if(isFirst)
                {
                    isFirst = false;
                }
                else
                {
                    // is last
                    refreshAdapter();
                    isFirst = true;// reset the flag
                }



//                if(swipeContainer!=null)
//                {
//                    swipeContainer.setRefreshing(false);
//                }

            }
        });
    }



    void refreshAdapter()
    {
        dataset.clear();

        HeaderItemsList headerItemCategory = new HeaderItemsList();
        headerItemCategory.setHeading(currentCategory.getCategoryName() + " Subcategories");
        dataset.add(headerItemCategory);

        dataset.addAll(datasetCategory);

        HeaderItemsList headerItem = new HeaderItemsList();
        headerItem.setHeading(currentCategory.getCategoryName() + " Items");
        dataset.add(headerItem);

        dataset.addAll(datasetItems);

        listAdapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);
    }




    void makeRequestItem(final boolean clearDataset, boolean resetOffset)
    {

        if(resetOffset)
        {
            offset_item = 0;
        }



//        (double)UtilityGeneral.getFromSharedPrefFloat(UtilityGeneral.LAT_CENTER_KEY),
//                (double)UtilityGeneral.getFromSharedPrefFloat(UtilityGeneral.LON_CENTER_KEY),


        String current_sort = "";

        current_sort = UtilitySortItemsByCategory.getSort(getContext()) + " " + UtilitySortItemsByCategory.getAscending(getContext());

        Call<ItemEndPoint> endPointCall = itemService.getItemsEndpoint(currentCategory.getItemCategoryID(),
                null,
                UtilityLocation.getLatitude(getActivity()),
                UtilityLocation.getLongitude(getActivity()),
                null,null, null, null,
                current_sort, limit_item,offset_item,null);


        endPointCall.enqueue(new Callback<ItemEndPoint>() {
            @Override
            public void onResponse(Call<ItemEndPoint> call, Response<ItemEndPoint> response) {


                if(isDestroyed)
                {
                    return;
                }


                if(clearDataset)
                {

                    if(response.body()!=null)
                    {

                        datasetItems.clear();
                        datasetItems.addAll(response.body().getResults());

//                        fetched_items_count = fetched_items_count + response.body().getResults().size();
//                        item_count_item = response.body().getItemCount();

                        item_count_item = response.body().getItemCount();
                        fetched_items_count = datasetItems.size();

//                        if(response.body().getItemCount()!=null)
//                        {
//
//                        }
                    }


                    if(isFirst)
                    {
                        isFirst = false;
                    }
                    else
                    {
                        // is last
                        refreshAdapter();
                        isFirst = true;// reset the flag
                    }

                }
                else
                {
                    if(response.body()!=null)
                    {

                        dataset.addAll(response.body().getResults());
                        fetched_items_count = fetched_items_count + response.body().getResults().size();
                        item_count_item = response.body().getItemCount();
                        listAdapter.notifyDataSetChanged();
                    }

                    swipeContainer.setRefreshing(false);
                }


                notifyItemHeaderChanged();


            }

            @Override
            public void onFailure(Call<ItemEndPoint> call, Throwable t) {

                if(isDestroyed)
                {
                    return;
                }


                if(clearDataset)
                {

                    if(isFirst)
                    {
                        isFirst = false;
                    }
                    else
                    {
                        // is last
                        refreshAdapter();
                        isFirst = true;// reset the flag
                    }
                }
                else
                {
                    swipeContainer.setRefreshing(false);
                }


                showToastMessage("Items: Network request failed. Please check your connection !");

            }
        });

    }





    @Override
    public void notifyRequestSubCategory(ItemCategory itemCategory) {

        ItemCategory temp = currentCategory;
        currentCategory = itemCategory;
        currentCategory.setParentCategory(temp);

        makeRefreshNetworkCall();

        resetPreviousPosition();
    }




    boolean backPressed = false;

    @Override
    public boolean backPressed() {

        // previous position is a variable used for tracking scrolling
        resetPreviousPosition();

        int currentCategoryID = 1; // the ID of root category is always supposed to be 1

        if(currentCategory!=null) {


            if (currentCategory.getParentCategory() != null) {

                currentCategory = currentCategory.getParentCategory();
                currentCategoryID = currentCategory.getItemCategoryID();

            } else {
                currentCategoryID = currentCategory.getParentCategoryID();
            }


            if (currentCategoryID != -1) {
                makeRefreshNetworkCall();
            }
        }



        return currentCategoryID == -1;
    }









    void notifyItemHeaderChanged()
    {
        if(getActivity() instanceof NotifyHeaderChanged)
        {
            ((NotifyHeaderChanged) getActivity()).notifyItemHeaderChanged(String.valueOf(fetched_items_count) + " out of " + String.valueOf(item_count_item) + " " + currentCategory.getCategoryName() + " Items");
        }



        if(currentCategory.getItemCategoryID()==1)
        {
            itemHeader.setText(String.valueOf(fetched_items_count) + " out of " + String.valueOf(item_count_item) + " Items");
        }
        else
        {
            itemHeader.setText(String.valueOf(fetched_items_count) + " out of " + String.valueOf(item_count_item) + " " + currentCategory.getCategoryName());
        }
//        + " Items"

    }




    @Override
    public void notifySortChanged() {

        makeRefreshNetworkCall();
    }


}