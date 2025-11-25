/*
 * opj_config_private.h - OpenJPEG 2.5.0 Private Configuration
 * Internal configuration for iOS build
 */

#ifndef OPJ_CONFIG_PRIVATE_H
#define OPJ_CONFIG_PRIVATE_H

/* Include public config */
#include "opj_config.h"

/* Private build flags */
#define OPJ_PACKAGE_VERSION "2.5.0"

/* Compiler support */
#if defined(__GNUC__) || defined(__clang__)
  #define OPJ_HAVE_MEMALIGN 0
  #define OPJ_HAVE_POSIX_MEMALIGN 1
#else
  #define OPJ_HAVE_MEMALIGN 0
  #define OPJ_HAVE_POSIX_MEMALIGN 0
#endif

/* Thread support */
#define OPJ_HAVE_PTHREAD 1

/* Math functions */
#define OPJ_HAVE_RINT 1

#endif /* OPJ_CONFIG_PRIVATE_H */
