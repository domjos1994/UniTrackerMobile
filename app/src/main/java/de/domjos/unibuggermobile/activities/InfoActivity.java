package de.domjos.unibuggermobile.activities;

import android.content.Intent;
import android.net.Uri;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import de.domjos.customwidgets.model.AbstractActivity;
import de.domjos.unibuggermobile.R;

public class InfoActivity extends AbstractActivity {
    public final static String TITLE = "title";
    public final static String CONTENT = "content";
    public final static String ABOUT = "about";
    private final static String HELP_URL = "https://domjos.de/kontakt.html";

    private FloatingActionButton floatingActionButton;

    public InfoActivity() {
        super(R.layout.info_activity);
    }

    @Override
    protected void initActions() {
        this.floatingActionButton.setOnClickListener(view -> {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(InfoActivity.HELP_URL));
            startActivity(browser);
        });
    }

    @Override
    protected void initControls() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        boolean about = false;
        String content = "";
        if(this.getIntent() != null) {
            if(this.getIntent().hasExtra(InfoActivity.TITLE)) {
                this.setTitle(this.getIntent().getStringExtra(InfoActivity.TITLE));
            }
            if(this.getIntent().hasExtra(InfoActivity.CONTENT)) {
                content = this.getIntent().getStringExtra(InfoActivity.CONTENT);
            }
            if(this.getIntent().hasExtra(InfoActivity.ABOUT)) {
                about = this.getIntent().getBooleanExtra(InfoActivity.ABOUT, false);
            }
        }

        ((TextView) this.findViewById(R.id.lblContent)).setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
        this.floatingActionButton = this.findViewById(R.id.fab);
        this.floatingActionButton.setVisibility(about ? View.VISIBLE : View.GONE);
    }
}