# Idle Device Management

Automatically turns off devices when not in use.

Designed to work with [taatta](https://github.com/MonashSmartCityLivingLab/taatta) device logger.

## Development

### Dependencies

- [JDK 17](https://adoptium.net/temurin/releases/)
- [Kotlin](https://kotlinlang.org/docs/getting-started.html)
- [Gradle](https://gradle.org/install/) (optional - wrapper included in repository)

In addition, you will need taata's `collector`, `athom-smart-plug` and `athom-presence-sensor` modules running at the 
minimum in order to receive smart plug and motion sensor states. See [taatta documentation](https://github.com/MonashSmartCityLivingLab/taatta#deployment)
for details.

### Building

1. Clone the repository
2. Copy your sites configuration at `config.json` in the repo root
   - Alternatively, pass `SITES_CONFIG` environment variable to specify the location of your sites config
   - A symlink of `config.json` to `config.example.json` has been provided as a fallback
3. Run `./gradlew build` to build

## Deploy

### Docker

**Note:** The `docker-compose.yml` file in this repo only launches the idle device management container. In order to 
deploy the full set of containers required, use the `docker-compose.yml` file on [taatta](https://github.com/MonashSmartCityLivingLab/taatta)
instead.

1. Clone the taatta repo

```
git clone https://github.com/MonashSmartCityLivingLab/taatta.git
cd taatta
```

2. Place your `config.json` at `configuration/idle-device-management/config.json` of taatta repo.
3. Follow the [taatta documentation](https://github.com/MonashSmartCityLivingLab/taatta#docker) deployment guide, and launch the containers 
(**Note:** the following command below is example only. Adjust it to your needs):

```
docker compose up --force-recreate postgresql mosquitto collector athom-smart-plug athom-presence-sensor idle-device-management device-control-interface
```

### Manual

A [systemd service file](idle-device-management.service)