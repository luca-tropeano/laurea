package com.example.examapplication;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResearchFragment extends Fragment {

    private static final String API_KEY = "AIzaSyDI0ejE9j2HxzSxY0bdLVHvO5iqcpgKtJA";
    private static final String SEARCH_ENGINE_ID = "1732f3bf1a653426d";
    private EditText editTextSearch;
    private Button searchButton;
    private WebView resultWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_research, container, false);

        editTextSearch = view.findViewById(R.id.editTextSearch);
        searchButton = view.findViewById(R.id.searchButton);
        resultWebView = view.findViewById(R.id.resultWebView);

        resultWebView.getSettings().setJavaScriptEnabled(true);

        resultWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = editTextSearch.getText().toString();
                Log.d("SearchFragment", "Query di ricerca: " + searchTerm);
                performSearch(searchTerm);
            }
        });

        return view;
    }

    private void performSearch(String query) {
        resultWebView.loadData("Ricerca in corso...", "text/html", "UTF-8");
        Log.d("SearchFragment", "Eseguo la ricerca con la query: " + query);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GoogleCustomSearchApi searchApi = retrofit.create(GoogleCustomSearchApi.class);
        Call<SearchResponse> call = searchApi.search(API_KEY, SEARCH_ENGINE_ID, query);

        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful()) {
                    List<SearchResponse.Item> items = response.body().getItems();

                    if (items != null && !items.isEmpty()) {
                        StringBuilder resultHtml = new StringBuilder();
                        resultHtml.append("<html><body>");

                        for (SearchResponse.Item item : items) {
                            String title = item.getTitle();
                            String link = item.getLink();
                            resultHtml.append("<p><strong>").append(title).append("</strong><br>")
                                    .append("<a href=\"").append(link).append("\">").append(link).append("</a></p>");
                        }

                        resultHtml.append("</body></html>");

                        resultWebView.loadData(resultHtml.toString(), "text/html", "UTF-8");
                    } else {
                        Log.d("SearchFragment", "Nessun risultato trovato");
                        Log.d("SearchFragment", "Risposta API: " + response.raw().toString());
                        resultWebView.loadData("Nessun risultato trovato", "text/html", "UTF-8");
                    }
                } else {
                    int statusCode = response.code();
                    Log.e("SearchFragment", "Errore nella ricerca. Codice: " + statusCode);
                    Log.e("SearchFragment", "Risposta API: " + response.raw().toString());

                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("SearchFragment", "Corpo dell'errore: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    resultWebView.loadData("Errore nella ricerca", "text/html", "UTF-8");
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                resultWebView.loadData("Errore di rete", "text/html", "UTF-8");
            }
        });
    }
}
