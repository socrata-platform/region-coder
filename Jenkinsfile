@Library('socrata-pipeline-library@testing') _

commonPipeline(
  defaultBuildWorker: 'build-worker',
  jobName: 'region-coder',
  language: 'scala',
  languageOptions: [
    version: '2.10',
    crossCompile: true,
    multiProjectBuild: false
  ],
  projects: [
    [
      name: 'region-coder',
      deploymentEcosystem: 'marathon-mesos',
      type: 'service',
      compile: true,  // Sane default
    ]
  ],
  teamsChannelWebhookId: 'WORKFLOW_IQ',
)
