@import self.exercise.personal_weather.models.GeoLocation
@import java.util.List

@param String title
@param List<GeoLocation> locs

@template.main(content = @`
    <form action="/getCities" method="get">
        <button type="submit">

        </button>
    </form>
`,
title = title
)
