package us.v4lk.transrock.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import java.util.HashSet;
import java.util.Set;

import us.v4lk.transrock.AddRoutesActivity;
import us.v4lk.transrock.R;
import us.v4lk.transrock.adapters.RouteAdapter;
import us.v4lk.transrock.transloc.Route;
import us.v4lk.transrock.util.Util;

/**
 * Created by qly on 8/11/15.
 */
public class RoutesFragment extends Fragment {

    ListView routeList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate layout
        View root = inflater.inflate(R.layout.fragment_routelist, container, false);

        // capture list
        routeList = (ListView) root.findViewById(R.id.routelist);

        // capture & setup fab
        FloatingActionButton addRoutesFab =
                (FloatingActionButton) root.findViewById(R.id.routelist_addroute_fab);
        addRoutesFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start add routes activity
                Intent intent = new Intent(getActivity(), AddRoutesActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup empty adapter
        routeList.setAdapter(new RouteAdapter(getActivity(), R.layout.route_list_item));
        // populate routelist
        updateRoutelist();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        updateRoutelist();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_routelist, menu);
    }

    private void updateRoutelist() {
        // get routes from db
        Set<Route> routes = Hawk.get(Util.ROUTES_STORAGE_KEY, new HashSet<Route>());

        // clear adapter
        RouteAdapter adapter = (RouteAdapter) routeList.getAdapter();
        adapter.clear();

        // populate list with user's routes, or hide list if there are none
        if (routes.size() != 0) {
            adapter.addAll(routes);
            routeList.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.routelist_noroutes_message).setVisibility(View.GONE);
        }
        else {
            routeList.setVisibility(View.GONE);
            getView().findViewById(R.id.routelist_noroutes_message).setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

}