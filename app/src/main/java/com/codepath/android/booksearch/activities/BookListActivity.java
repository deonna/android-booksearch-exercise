package com.codepath.android.booksearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.codepath.android.booksearch.R;
import com.codepath.android.booksearch.adapters.BookAdapter;
import com.codepath.android.booksearch.models.Book;
import com.codepath.android.booksearch.net.BookClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import cz.msebera.android.httpclient.Header;


public class BookListActivity extends AppCompatActivity {

    public static final String KEY_BOOK = "book";

    @BindView(R.id.lvBooks) ListView lvBooks;
    @BindView(R.id.tbBooks) Toolbar tbBooks;

    private MenuItem miActionProgressItem;

    private BookClient client;
    private BookAdapter bookAdapter;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
        ButterKnife.bind(this);

        setSupportActionBar(tbBooks);

        ArrayList<Book> aBooks = new ArrayList<>();
        // initialize the adapter
        bookAdapter = new BookAdapter(this, aBooks);
        // attach the adapter to the ListView
        lvBooks.setAdapter(bookAdapter);
        // Fetch the data remotely
        fetchBooks("");

    }

    @OnItemClick(R.id.lvBooks)
    public void openDetailView(AdapterView<?> parent, View view, int position, long id) {

        Book book = bookAdapter.getItem(position);

        Intent intent = new Intent(BookListActivity.this, BookDetailActivity.class);
        intent.putExtra(KEY_BOOK, book);
        startActivity(intent);
    }

    // Executes an API call to the OpenLibrary search endpoint, parses the results
    // Converts them into an array of book objects and adds them to the adapter
    private void fetchBooks(String query) {
        client = new BookClient();
        client.getBooks(query, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray docs;
                    if(response != null) {
                        // Get the docs json array
                        docs = response.getJSONArray("docs");
                        // Parse json array into array of model objects
                        final ArrayList<Book> books = Book.fromJson(docs);
                        // Remove all books from the adapter
                        bookAdapter.clear();
                        // Load model objects into the adapter
                        for (Book book : books) {
                            bookAdapter.add(book); // add book through the adapter
                        }
                        bookAdapter.notifyDataSetChanged();
                        hideProgressBar();
                    }
                } catch (JSONException e) {
                    // Invalid JSON format, show appropriate error.
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchBooks(query);
                searchView.clearFocus();

                showProgressBar();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        miActionProgressItem = menu.findItem(R.id.miActionProgress);

        ProgressBar progressBar = (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);

        return super.onPrepareOptionsMenu(menu);
    }


    public void showProgressBar() {

        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {

        miActionProgressItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
