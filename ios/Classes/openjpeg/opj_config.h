/*
 * opj_config.h - OpenJPEG 2.5.0 Configuration Header
 * Configured for iOS static build
 */

#ifndef OPJ_CONFIG_H
#define OPJ_CONFIG_H

/* OpenJPEG version */
#define OPJ_VERSION_MAJOR 2
#define OPJ_VERSION_MINOR 5
#define OPJ_VERSION_BUILD 0

/* Platform detection */
#if defined(__APPLE__)
  #define OPJ_HAVE_FSEEKO
#endif

/* Feature flags */
#define OPJ_HAVE_LIBPNG 0
#define OPJ_HAVE_LIBTIFF 0
#define OPJ_HAVE_LIBLCMS2 0

/* Build type */
#define OPJ_STATIC 1

/* Standard headers */
#define OPJ_HAVE_STDINT_H 1
#define OPJ_HAVE_INTTYPES_H 1

#endif /* OPJ_CONFIG_H */
