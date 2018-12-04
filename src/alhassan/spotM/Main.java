package alhassan.spotM;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Album;

import java.io.IOException;
import java.util.Arrays;

public class Main {

   // private static final String accessToken = "taHZ2SdB-bPA3FsK3D7ZN5npZS47cMy-IEySVEGttOhXmqaVAIo0ESvTCLjLBifhHOHOIuhFUKPW1WMDP7w6dj3MAZdWT8CLI2MkZaXbYLTeoDvXesf2eeiLYPBGdx8tIwQJKgV8XdnzH_DONk";
    private static final String id = "5zT1JLIj9E57p3e1rFm9Uq";

    public static void main(String[] args) {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId("33b1c0aaa872462eb101998593ecbbae")
                .setClientSecret("2fb7ec1e27534c4485ffb311508a3090")
                .setRedirectUri(SpotifyHttpManager.makeUri("https://o6tpuo57vg.execute-api.us-east-1.amazonaws.com/default/echo_fun"))
                .build();



        var  searchAlbumsRequest = spotifyApi.searchAlbums("the beatles")
                .build();


        try {
            var creds = spotifyApi.clientCredentials().build().execute();
            spotifyApi.setAccessToken(creds.getAccessToken());

            var search = spotifyApi.searchAlbums("the beatles").build().execute();
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }




}
