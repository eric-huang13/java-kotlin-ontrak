package com.insperity.escmobile.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.insperity.escmobile.R;
import com.insperity.escmobile.action.creators.LegalActionsCreator;
import com.insperity.escmobile.event.OnAboutItems;
import com.insperity.escmobile.store.LegalStore;
import com.insperity.escmobile.util.AppUtil;
import com.insperity.escmobile.view.activity.AboutWebActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutFragment extends BaseFragment {

    @Inject
    EventBus bus;
    @Inject
    LegalActionsCreator actionsCreator;
    @Inject
    LegalStore store;
    @Inject
    AppUtil appUtil;

    @BindView(R.id.version_text)
    TextView version;

    @BindView(R.id.list)
    ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getComponent().inject(this);
        version.setText(appUtil.getVersionInfoFull());
        actionsCreator.getLegal();
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause() {
        bus.unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onEvent(OnAboutItems event) {
        listView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.listview_item, event.aboutItemArray));

        listView.setOnItemClickListener((adapterView, view, pos, l) -> {
            String title = store.getPageTitle(pos);
            String htmlPage = store.getPageContent(pos);
            startActivity(AboutWebActivity.Companion.getCallingIntent(getActivity(), title, htmlPage));
        });

        showProgressBar(false);
    }

}
