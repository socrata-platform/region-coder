@Library('socrata-pipeline-library@generalize-sbt-to-work-for-multiple-projects') _

commonPipeline(
  defaultBuildWorker: 'build-worker',
  jobName: 'region-coder',
  language: 'scala',
  languageOptions: [
    crossCompile: true,
    isMultiProjectRepository: false
  ],
  projects: [
    [
      name: 'region-coder',
      deploymentEcosystem: 'marathon-mesos',
      type: 'service',
      compiled: true,
      paths: [
        dockerBuildContext: 'docker'
      ]
    ]
  ],
  teamsChannelWebhookId: 'WORKFLOW_IQ',
)
