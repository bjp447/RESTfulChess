package pl.art.lach.mateusz.javaopenchess.model;

/*
* Response to return a simple string wrapped in the JSON format
 */
public class DescriptionResponse implements Response
{
    private final String description;

    public DescriptionResponse() {
        this.description = "";
    }

    public DescriptionResponse(String description) {
        this.description = description;
    }
    //getters for Jackson JSON library that turns objects into JSON
    public String getDescription() { return description; }
}
