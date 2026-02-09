# Enable Gitea Actions Cache to Accelerate CI/CD

[![](/gitea-text.svg)Gitea](/)

Open main menu

Products

[Gitea Cloud](/products/cloud/)

Get a DevOps instance in minutes

[Gitea Enterprise](/products/gitea-enterprise/)

Run an enhanced DevOps instance yourself

[Gitea](/products/gitea/)

Run a free DevOps instance yourself

[Gitea Actions](/products/runner/)

Automate your Gitea workflows

[Tea](/products/tea/)

Command line tool to interact with Gitea Servers

Resources

[News](/news)

What happened around CommitGo and Gitea

[Documentations](https://docs.gitea.com)

Documentation for Gitea and related tools

[Tutorials](/resources/tutorials/)

Tutorals and advice on using Gitea

[Blog](/blog)

Release notes and updates about Gitea Products

Community

[Forum](https://forum.gitea.com)

Find or help out with community support

[Chatroom](https://discord.gg/gitea)

Chat with the community

[Open Source](https://github.com/go-gitea/gitea)

View Gitea code and contribute development

[Community Blog](https://blog.gitea.com)

Release notes and updates about Gitea

[Translation](https://translate.gitea.com)

Help translate Gitea

[Supporters](/community/supporters/)

View supporters of Gitea

[Pricing](/pricing)[Cloud](https://cloud.gitea.com)

[Sign In](https://gitea.com/user/login)     [Contact Us](/contact/contact)

![](/img/tutorials/enable-gitea-actions-cache-to-accelerate-cicd/cover.jpg)

# Enable Gitea Actions Cache to Accelerate CI/CD

![](https://github.com/lng2020.png)

[Nanguan Lin](https://github.com/lng2020)

2023-10-26

4 min read

[CI/CD](/resources/tutorials?category=cicd)

## Introduction

Caching is a vital aspect of modern computer science. Today, we will discuss enabling Gitea Actions(Gitea's built-in CI/CD) cache to speed up CI/CD.

Gitea Actions utilizes two types of caches. The first is the Runner Tool Cache, created when launching a runner. This runner creates a volume named `act-toolcache`, which is mounted to the local file system(usually `/opt/hostedtoolcache`). When an action like `setup-go` is used, it downloads and installs a version of Go, storing it in this special volume, thus preventing redundant downloads of dependencies.

The second type is more fine-grained. Originating from Github Actions but compatible with Gitea Actions, it's called `action/cache`. This action uses a hash key to retrieve the specific cache. For more specific information and detailed configuration about this action, refer to this [Github Offical Doc](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows). In this tutorial, we will enable both types of caches to accelerate CI/CD.

## Use Runner Tool Cache

The process is straightforward. Just add an `env` variable called `RUNNER_TOOL_CACHE` in your Gitea action workflow, and the Gitea act runner will automatically detect this environment and store the download cache there.

**Notice**: For now, `/toolcache` is [hardcoded](https://github.com/nektos/act/blob/4fae81efe4cdd9e09e7ef8e874a2d63b1ed98524/pkg/runner/run_context.go#L137-L139) in the upstream project `nektos/act`. So it cannot be changed.

An example configuration:

`jobs:   build:    env:      RUNNER_TOOL_CACHE: /toolcache ...`

Alternatively, you can use `Docker Volume` to specifically mount the cache volume:

`jobs:   build:    runs-on: ubuntu-latest    container:      image: your_docker_image      volumes:        - your_docker_volumn:/opt/hostedtoolcache # this is where Runner store their cache default`

download time before: ![download_before](/img/tutorials/enable-gitea-actions-cache-to-accelerate-cicd/download_before.png)

download time after: ![download_after](/img/tutorials/enable-gitea-actions-cache-to-accelerate-cicd/download_after.png)

## Use Cache Action

The Runner uses a cache server to store the key/value pair cache. The cache server is enabled by default. So You can directly use the `action/cache`.

An example configuration:

`name: Caching with Go on: push jobs:   Cache-Go:    name: Cache Go    runs-on: ubuntu-latest    steps:      - uses: actions/checkout@v3      - uses: actions/setup-go@v3        with:          go-version: '>=1.20.1'      - uses: https://gitea.com/actions/go-hashfiles@v0.0.1        id: hash-go        with:          patterns: |            go.mod            go.sum      - name: cache go        id: cache-go        uses: actions/cache@v3        with: # Specify with your cache path          path: |            /your_cache_path          key: go_path-${{ steps.hash-go.outputs.hash }}          restore-keys: |-            go_cache-${{ steps.hash-go.outputs.hash }}`

This example utilizes a go cache and `go-hashfiles` to generate a hash. You should specify your cache path according to your programming language and define the key in any form you like.

**Notice**

1.  If you are running the Runner with docker. You may encounter network issue with the cache server. You should change the cache server host and port in `config.yaml` for your Act Runner. The configuration is explained [here](https://docs.gitea.com/usage/actions/act-runner#configuring-cache-when-starting-a-runner-using-docker-image) in detail.
2.  The built-in function `hashFiles` in the workflow yaml is not supported in Gitea Actions right now. You can use [`go-hashfiles`](https://gitea.com/actions/go-hashfiles)(maintained by Gitea maintainers) or other alternatives instead.

## A Complete Example

Let's use an example to demonstrate how to utilize these two types of caches in a real development environment.

Assume we're going to build an app called `Hello-Gitea` using Go, and we enable the Gitea Actions workflow for every Push.

Here is the workflow yaml(this file is also available [on Gitea website](https://gitea.com/lng2020/cache_example/src/branch/main/.gitea/workflows/cache.yaml))

`name: Test Cache on:    push jobs:   TestCache:    env:      RUNNER_TOOL_CACHE: /toolcache # Runner Tool Cache    name: Cache Go    runs-on: ubuntu-latest    steps:      - uses: actions/checkout@v3       - uses: actions/setup-go@v3        with:          go-version: '>=1.20.1'       - name: Get go-hashfiles        uses: https://gitea.com/actions/go-hashfiles@v0.0.1        id: hash-go        with:          patterns: |-            go.mod            go.sum                                                   - name: Echo hash        run: echo ${{ steps.hash-go.outputs.hash }}               - name: Cache go        id: cache-go        uses: https://github.com/actions/cache@v3 # Action cache        with: # specify with your GOMODCACHE and GOCACHE          path: |-            /root/go/pkg/mod            /root/.cache/go-build          key: go_cache-${{ steps.hash-go.outputs.hash }}          restore-keys: |-            go_cache-${{ steps.hash-go.outputs.hash }}                   - name: Build        run: go build -v .       - name: Test        run: go test -v ./...`

After setting everything up, we can see how these caches are utilized.

![tool cache](/img/tutorials/enable-gitea-actions-cache-to-accelerate-cicd/tool_cache.png)

![action cache](/img/tutorials/enable-gitea-actions-cache-to-accelerate-cicd/action_cache.png)

## FAQ

Q: Why should I specify `RUNNER_TOOL_CACHE: /toolcache` to make the Runner Tool Cache work? It seems like it should cache the file by default

A: It's an upstream issue of `nektos/act`. See the [issue](https://gitea.com/gitea/act_runner/issues/70) for more details.

Q: Can different act runners on the same host share the Runner Tool Cache?

A: Yes, they can. To do so, use a Docker volume to map the cache directory.

## Footer

![SOC 2 Type 2 Certified](/img/SOC2-blue.png)

Private, Fast, Reliable DevOps Platform

[LinkedIn](https://linkedin.com/company/commitgo)[X](https://twitter.com/giteaio)[GitHub](https://github.com/go-gitea/gitea)[Gitea](https://gitea.com/gitea)

© 2026 CommitGo, Inc. All rights reserved.

### Products

*   [Gitea Cloud](/products/cloud)
*   [Gitea Enterprise](/products/gitea-enterprise)
*   [Gitea](/products/gitea)
*   [Gitea Runner](/products/runner)
*   [Tea Command-line Tool](/products/tea)

### Support

*   [Pricing](/pricing/)
*   [Documentation](https://docs.gitea.com)
*   [Tutorials](/resources/tutorials/)
*   [API](https://docs.gitea.com/api/1.21/)
*   [Blog](https://blog.gitea.com)
*   [Forum](https://forum.gitea.com)
*   [Chatroom](https://discord.gg/gitea)

### About Us

*   [What is DevOps](/about/devops)
*   [Why Gitea](/about/whygitea)
*   [Contact Us](/contact/contact)
*   [Compliance](/about/compliance)

### Legal

*   [Privacy](/privacy-policy/)
*   [Terms](/terms-of-service/)