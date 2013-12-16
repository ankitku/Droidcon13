package com.nkt.geomessenger;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nkt.geomessenger.model.ListItemWithIcon;
import com.nkt.geomessenger.model.ListviewAdapter;

public class MessagesActivity extends GMActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages_list);

		final ListView listview = (ListView) findViewById(R.id.list_view);

		List<ListItemWithIcon> tabs = new ArrayList<ListItemWithIcon>();
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact,
				"Nearby Messages", null));
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact, "Sent",
				null));
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact,
				"Invite Friends", null));
		tabs.add(new ListItemWithIcon(R.drawable.placeholder_contact,
				"Feedback", null));
		ListviewAdapter menuAdapter = new ListviewAdapter(
				MessagesActivity.this, tabs);

		listview.setAdapter(menuAdapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);
				view.animate().setDuration(2000).alpha(0)
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								//list.remove(item);
								//adapter.notifyDataSetChanged();
								view.setAlpha(1);
							}
						});
			}

		});
	}
}
