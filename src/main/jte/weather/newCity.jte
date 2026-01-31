@import self.exercise.personal_weather.models.GeoLocation
@import java.util.List

@param String title
@param List<GeoLocation> locs

@template.main(content = @`
    <form action="" method="post">
        <button type="submit">

        </button>
    </form>
`,
title = title
)
