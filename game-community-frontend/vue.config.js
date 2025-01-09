const { defineConfig } = require('@vue/cli-service');
const path = require('path');

module.exports = defineConfig({
    transpileDependencies: true,
    lintOnSave: false,
    publicPath: '/',
    outputDir: 'dist',
    assetsDir: 'static',
    productionSourceMap: false,

    devServer: {
        port: process.env.PORT || 8081,
        open: true,
        proxy: {
            [process.env.VUE_APP_BASE_API]: {
                target: process.env.VUE_APP_TARGET,
                changeOrigin: true,
                pathRewrite: {
                    ['^' + process.env.VUE_APP_BASE_API]: ''
                }
            },
            '/ws': {
                target: process.env.VUE_APP_TARGET,
                changeOrigin: true,
                ws: true
            }
        },
        client: {
            overlay: {
                warnings: false,
                errors: true
            }
        }
    },

    configureWebpack: {
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src')
            }
        },
        performance: {
            hints: false
        }
    },

    chainWebpack(config) {
        // 设置 svg-sprite-loader
        config.module
            .rule('svg')
            .exclude.add(path.resolve('src/icons'))
            .end();
        config.module
            .rule('icons')
            .test(/\.svg$/)
            .include.add(path.resolve('src/icons'))
            .end()
            .use('svg-sprite-loader')
            .loader('svg-sprite-loader')
            .options({
                symbolId: 'icon-[name]'
            })
            .end();

        // 设置项目名称
        config
            .plugin('html')
            .tap(args => {
                args[0].title = '游戏社区';
                return args;
            });

        // 生产环境配置
        if (process.env.NODE_ENV === 'production') {
            // 去除console
            config.optimization
                .minimize(true)
                .minimizer('terser')
                .tap(args => {
                    args[0].terserOptions.compress.drop_console = true;
                    args[0].terserOptions.compress.drop_debugger = true;
                    return args;
                });

            // Gzip压缩
            config
                .plugin('compression-webpack-plugin')
                .use(require('compression-webpack-plugin'), [{
                    filename: '[path][base].gz',
                    algorithm: 'gzip',
                    test: /\.js$|\.css$|\.html$/,
                    threshold: 10240,
                    minRatio: 0.8
                }]);
        }
    }
});