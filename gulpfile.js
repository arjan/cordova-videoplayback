var gulp = require('gulp');
var gutil = require('gulp-util');
var rename = require('gulp-rename');
var sh = require('shelljs');

var paths = {
  android: ['./src/android/**'],
  www: ['./www/**'],
};

gulp.task('default', ['android', 'www']);

gulp.task('android', function(done) {
  sh.cp('-Rf', 'src/android/*', '../testapp/platforms/android/src/nl/miraclethings/videoplayback/');
  done();
});

gulp.task('www', function(done) {
  sh.cp('-Rf', 'www/*', '../testapp/platforms/android/assets/www/plugins/nl.miraclethings.videoplayback/www/');
  done();
});


gulp.task('watch', function() {
  gulp.watch(paths.android, ['android']);
  gulp.watch(paths.www, ['www']);
});

