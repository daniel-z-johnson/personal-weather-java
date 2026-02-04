@import self.exercise.personal_weather.models.GeoLocation
@import java.util.List
@import self.exercise.personal_weather.models.GeoLocation

@param String title
@param List<GeoLocation> cities

@template.main(content = @`
    <form action="/cities" method="POST">
        <label for="city">City Name:</label>
        <input type="text" id="city" name="city" required>
        <button type="submit" ">Submit</button>
    </form>
    @if(cities != null)
        @for(GeoLocation city: cities)
           <div>
                ${city.getName()}, ${city.getState()}, ${city.getCountry()}
           </div>
        @endfor
    @endif
`,
title = title
)
