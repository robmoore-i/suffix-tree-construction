job("Build") {
    container("openjdk:11") {
        this@job.gradlew("openjdk:11", "build")
        this@job.docker {
            build {
                file = "./docker/Dockerfile"
                args["target"] = "lsystems"
            }

            push("codespyglass.registry.somewhere.space/p/analyser/containers/lsystems") {
                tag = "latest"
            }
        }
    }
}