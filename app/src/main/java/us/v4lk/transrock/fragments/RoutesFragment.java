package us.v4lk.transrock.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import us.v4lk.transrock.R;
import us.v4lk.transrock.SelectRoutesActivity;
import us.v4lk.transrock.adapters.RouteAdapter;
import us.v4lk.transrock.model.RouteModel;

/**
 * Route list fragment.
 */
public class RoutesFragment extends Fragment {

    @Bind(R.id.routelist)
    ListView routeList;
    @Bind(R.id.routelist_addroute_fab)
    FloatingActionButton fab;
    @Bind(R.id.routelist_noroutes_message)
    View noRoutesMessage;

    Realm realm;

    @OnClick(R.id.routelist_addroute_fab)
    void onFabClick() {
        // start add routes activity
        Intent intent = new Intent(getActivity(), SelectRoutesActivity.class);
        startActivity(intent);
    }


    /* lifecycle */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_routelist, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RouteAdapter adapter = new RouteAdapter(getActivity(), R.layout.route_list_item);
        routeList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onResume() {
        realm.beginTransaction();

        // pull routes from realm and update list
        RouteAdapter adapter = (RouteAdapter) routeList.getAdapter();
        adapter.clear();
        adapter.addAll(realm.allObjects(RouteModel.class));
        adapter.notifyDataSetChanged();

        // update UI if list is empty or full
        if (adapter.isEmpty()) {
            routeList.setVisibility(View.GONE);
            noRoutesMessage.setVisibility(View.VISIBLE);
        }
        else {
            routeList.setVisibility(View.VISIBLE);
            noRoutesMessage.setVisibility(View.GONE);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        realm.commitTransaction();
        super.onPause();
    }

    @Override
    public void onStop() {
        realm.close();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_routelist, menu);
    }

}
