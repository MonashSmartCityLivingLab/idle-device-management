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

## Sites configuration

In order for the system to run, you'll need to supply a sites configuration which lists rooms, smart plugs and motion sensors.

In general, it is a JSON file which contains an array of sites, which contain an array of rooms, which itself contain an
array of appliances and motion sensors. 

For each appliance, it has a standard use times which are the typical times that the appliance is used.

An example sites configuration can be found at `sites.example.json`

### Sites configuration notes

- `startTime` and `endTime` must be specified as 24-hour format, in `HH:mm` format
  - If the hour or minute is 0 to 9, they need to be zero padded, i.e. `09:00` instead of `9:00`
- All attributes must be present on each item. If there are no motion sensors in a room, put an empty array (`[]`) instead
- If you want to have some device controlled by motion sensors and others not controlled by one, then you need to split the room into 2
- When a standard use time spans across days, break it up on midnight, i.e. `start -> 23:59` and `00:00 -> end`

## Licence

Licensed under [Apache License 2.0](LICENSE).
