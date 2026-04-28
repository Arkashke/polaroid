package polaroid.client.utils.shader;

public interface IShader {

    String glsl();

    default String getName() {
        return "SHADERNONAME";
    }

}


