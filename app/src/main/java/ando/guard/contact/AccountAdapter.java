package ando.guard.contact;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import ando.guard.R;

/**
 * Custom adapter used to display account icons and descriptions in the account spinner.
 */
public class AccountAdapter extends ArrayAdapter<ContactAdder.AccountData> {
    public AccountAdapter(Context context, ArrayList<ContactAdder.AccountData> accountData) {
        super(context, android.R.layout.simple_spinner_item, accountData);
        setDropDownViewResource(R.layout.account_entry);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NotNull ViewGroup parent) {
        // Inflate a view template
        if (convertView == null) {
            LayoutInflater layoutInflater = ((Activity) getContext()).getLayoutInflater();
            convertView = layoutInflater.inflate(R.layout.account_entry, parent, false);
        }
        TextView firstAccountLine = (TextView) convertView.findViewById(R.id.firstAccountLine);
        TextView secondAccountLine = (TextView) convertView.findViewById(R.id.secondAccountLine);
        ImageView accountIcon = (ImageView) convertView.findViewById(R.id.accountIcon);

        // Populate template
        ContactAdder.AccountData data = getItem(position);
        firstAccountLine.setText(data.getName());
        secondAccountLine.setText(data.getTypeLabel());
        Drawable icon = data.getIcon();
        if (icon == null) {
            icon = ResourcesCompat.getDrawable(getContext().getResources(), android.R.drawable.ic_menu_search, getContext().getTheme());
        }
        accountIcon.setImageDrawable(icon);
        return convertView;
    }
}