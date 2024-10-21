const { withAndroidManifest } = require('@expo/config-plugins');

module.exports = function withPhotoPicker(_config) {
  return withAndroidManifest(_config, async (config) => {
    const androidManifest = config.modResults.manifest;

    // Define the service entry that we want to add to the manifest
    const photoPickerService = {
      '$': {
        'android:name': 'com.google.android.gms.metadata.ModuleDependencies',
        'android:enabled': 'false',
        'android:exported': 'false',
      },
      'intent-filter': [
        {
          action: [
            {
              $: {
                'android:name':
                  'com.google.android.gms.metadata.MODULE_DEPENDENCIES',
              },
            },
          ],
        },
      ],
      'meta-data': [
        {
          $: {
            'android:name': 'photopicker_activity:0:required',
            'android:value': '',
          },
        },
      ],
    };

    // Ensure we only add the service if it doesn't already exist
    if (!androidManifest.application[0].service) {
      androidManifest.application[0].service = [];
    }

    androidManifest.application[0].service.push(photoPickerService);

    return config;
  });
};
