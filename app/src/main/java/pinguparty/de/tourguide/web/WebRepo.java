package pinguparty.de.tourguide.web;

import pinguparty.de.tourguide.web.models.Person;
import pinguparty.de.tourguide.web.models.Poi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Marvin on 02.02.2018.
 */

public interface WebRepo {
    @GET("/api/people/{nr}")
    Call<Person> getPerson(@Path("nr")int persNr);

    @POST("/api/v1/poi")
    Call<Poi> createPoi(@Body Poi newpoi);
}
