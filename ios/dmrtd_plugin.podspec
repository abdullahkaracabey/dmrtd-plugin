#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint dmrtd_plugin.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'dmrtd_plugin'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin project.'
  s.description      = <<-DESC
A new Flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*.{h,m,mm,swift}', 'Classes/openjpeg/*.c'
  # Exclude test files and JPIP server components (not needed for decoding)
  s.exclude_files = 'Classes/openjpeg/bench_dwt.c', 'Classes/openjpeg/t1_generate_luts.c', 'Classes/openjpeg/test_sparse_array.c', 'Classes/openjpeg/*_manager.c', 'Classes/openjpeg/cidx_manager.c', 'Classes/openjpeg/phix_manager.c', 'Classes/openjpeg/ppix_manager.c', 'Classes/openjpeg/thix_manager.c', 'Classes/openjpeg/tpix_manager.c'
  s.public_header_files = 'Classes/**/*.h'
  s.private_header_files = 'Classes/openjpeg/*.h'
  s.dependency 'Flutter'
  s.dependency 'OpenSSL-Universal', '1.1.2301'
  s.dependency 'QKMRZScanner'
  s.frameworks = 'UIKit', 'Foundation', 'CoreGraphics'
  s.platform = :ios, '15.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386',
    'HEADER_SEARCH_PATHS' => '$(PODS_TARGET_SRCROOT)/Classes/openjpeg',
    'GCC_PREPROCESSOR_DEFINITIONS' => 'OPJ_STATIC=1'
  }
  s.swift_version = '5.0'
end
